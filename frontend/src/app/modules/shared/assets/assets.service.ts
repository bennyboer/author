import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments';
import { Asset } from './asset';
import { Option } from '../util';

type AssetId = string;

interface CreateAssetRequest {
  contentType: string;
  content: string;
}

interface AssetDTO {
  id: AssetId;
  version: number;
  content: string;
  contentType: string;
  createdAt: number;
}

@Injectable()
export class AssetsService {
  constructor(private readonly http: HttpClient) {}

  create(content: string, contentType: string): Observable<AssetId> {
    const request: CreateAssetRequest = {
      contentType,
      content,
    };

    return this.http
      .post<AssetId>(this.url(''), request, {
        observe: 'response',
      })
      .pipe(
        map((response) =>
          Option.someOrNone(response.headers.get('Location'))
            .flatMap((location) => Option.someOrNone(location.split('/').pop()))
            .orElseThrow(),
        ),
      );
  }

  fetch(id: AssetId): Observable<Asset> {
    return this.http.get<AssetDTO>(this.url(id)).pipe(
      map((asset) => {
        const createdAt = new Date();
        createdAt.setTime(asset.createdAt);

        return new Asset(
          asset.id,
          asset.version,
          asset.content,
          asset.contentType,
          createdAt,
        );
      }),
    );
  }

  private url(postfix: string): string {
    return `${environment.apiUrl}/assets/${postfix}`;
  }
}
