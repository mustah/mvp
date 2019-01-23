import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {SelectionTreeViewComposite} from '../../../state/ui/selection-tree/selectionTreeModels';
import {OnClickWithId} from '../../../types/Types';
import {addToReport} from '../../report/reportActions';
import {TreeViewListItemFolder} from '../components/TreeViewListItemFolder';

interface DispatchToProps {
  addToReport: OnClickWithId;
}

export type TreeViewListItemProps = DispatchToProps & SelectionTreeViewComposite;

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  addToReport,
}, dispatch);

export const TreeViewListItemFolderContainer =
  connect<{}, DispatchToProps, SelectionTreeViewComposite>(null, mapDispatchToProps)(TreeViewListItemFolder);
