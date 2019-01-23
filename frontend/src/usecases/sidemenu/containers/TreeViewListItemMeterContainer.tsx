import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {SelectionTreeViewComposite} from '../../../state/ui/selection-tree/selectionTreeModels';
import {OnClickWithId} from '../../../types/Types';
import {addToReport} from '../../report/reportActions';
import {TreeViewListItemMeter} from '../components/TreeViewListItemMeter';

interface DispatchToProps {
  addToReport: OnClickWithId;
}

export type TreeViewMeterListItemProps = DispatchToProps & SelectionTreeViewComposite;

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  addToReport,
}, dispatch);

export const TreeViewListItemMeterContainer =
  connect<{}, DispatchToProps, SelectionTreeViewComposite>(null, mapDispatchToProps)(TreeViewListItemMeter);
