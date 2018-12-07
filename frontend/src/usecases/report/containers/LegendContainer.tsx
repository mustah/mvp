import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {componentOrNothing} from '../../../components/hoc/hocs';
import {RootState} from '../../../reducers/rootReducer';
import {Normalized} from '../../../state/domain-models/domainModels';
import {OnClickWithId} from '../../../types/Types';
import {Legend, LegendProps} from '../components/Legend';
import {toggleLine, toggleSingleEntry} from '../reportActions';
import {LegendItem, ReportState} from '../reportModels';
import {getLegendItems} from '../reportSelectors';

interface StateToProps extends ReportState {
  legendItems: Normalized<LegendItem>;
}

interface DispatchToProps {
  deleteItem: OnClickWithId;
  toggleLine: OnClickWithId;
}

const hasSelectedItems = ({selectedListItems}: ReportState): boolean => selectedListItems.length > 0;

const LegendComponent = componentOrNothing<LegendProps & ReportState>(hasSelectedItems)(Legend);

const mapStateToProps = ({
  report: {hiddenLines, selectedListItems},
  selectionTree: {entities},
}: RootState): StateToProps =>
  ({
    legendItems: getLegendItems({selectedListItems, entities}),
    hiddenLines,
    selectedListItems,
  });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  deleteItem: toggleSingleEntry,
  toggleLine,
}, dispatch);

export const LegendContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(LegendComponent);
