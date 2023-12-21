import { TreeGraphCommand } from './command';
import { TreeGraphCommandType } from './type';
import { TreeGraphNodeId } from '../tree-graph.component';

export class RenameNodeCommand implements TreeGraphCommand {
  readonly type = TreeGraphCommandType.RENAME_NODE;

  constructor(
    public readonly nodeId: TreeGraphNodeId,
    public readonly name: string,
  ) {}
}
