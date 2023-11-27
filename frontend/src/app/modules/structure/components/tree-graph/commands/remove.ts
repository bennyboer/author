import { TreeGraphCommand } from './command';
import { TreeGraphCommandType } from './type';
import { TreeGraphNodeId } from '../tree-graph.component';

export class RemoveNodeCommand implements TreeGraphCommand {
  readonly type = TreeGraphCommandType.REMOVE_NODE;
  constructor(public readonly nodeId: TreeGraphNodeId) {}
}
