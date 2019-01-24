import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {SelectionTreeViewComposite} from '../../../state/ui/selection-tree/selectionTreeModels';
import {OnClickWithId} from '../../../types/Types';
import {toggleGroupItems} from '../../report/reportActions';
import {TreeViewListItemFolder} from '../components/TreeViewListItemFolder';

interface DispatchToProps {
  addToReport: OnClickWithId;
}

export type TreeViewListItemProps = DispatchToProps & SelectionTreeViewComposite;

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  addToReport: toggleGroupItems,
}, dispatch);

export const TreeViewListItemGroupContainer =
  connect<{}, DispatchToProps, SelectionTreeViewComposite>(null, mapDispatchToProps)(TreeViewListItemFolder);
