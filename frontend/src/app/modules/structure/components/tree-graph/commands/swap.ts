import { TreeGraphCommand } from './command';
import { TreeGraphCommandType } from './type';
import { TreeGraphNodeId } from '../tree-graph.component';

export class SwapNodesCommand implements TreeGraphCommand {
  readonly type = TreeGraphCommandType.SWAP_NODES;
  constructor(
    public readonly nodeId1: TreeGraphNodeId,
    public readonly nodeId2: TreeGraphNodeId,
  ) {}
}
