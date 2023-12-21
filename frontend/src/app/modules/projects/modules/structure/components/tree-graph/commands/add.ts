import { TreeGraphCommand } from './command';
import { TreeGraphCommandType } from './type';
import { TreeGraphNodeId } from '../tree-graph.component';

export class AddNodeCommand implements TreeGraphCommand {
  readonly type = TreeGraphCommandType.ADD_NODE;
  constructor(
    public readonly parentNodeId: TreeGraphNodeId,
    public readonly name: string,
  ) {}
}
