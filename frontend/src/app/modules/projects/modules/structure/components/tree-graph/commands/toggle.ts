import { TreeGraphCommand } from './command';
import { TreeGraphCommandType } from './type';
import { TreeGraphNodeId } from '../tree-graph.component';

export class ToggleNodeCommand implements TreeGraphCommand {
  readonly type = TreeGraphCommandType.TOGGLE_NODE;
  constructor(public readonly nodeId: TreeGraphNodeId) {}
}
