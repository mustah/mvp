import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Period} from '../../../components/dates/dateModels';
import {withContent} from '../../../components/hoc/withContent';
import {RootState} from '../../../reducers/rootReducer';
import {Medium} from '../../../state/ui/graph/measurement/measurementModels';
import {HasContent, OnClick, OnClickWith, OnClickWithId, Visible} from '../../../types/Types';
import {Legend} from '../components/Legend';
import {deleteItem, showHideAllByMedium, removeAllByMedium, showHideMediumRows, toggleLine} from '../reportActions';
import {LegendItem, MediumViewOptions, ReportState} from '../reportModels';
import {getLegendItems, getMediumViewOptions} from '../reportSelectors';

export interface StateToProps extends ReportState, HasContent {
  legendItems: LegendItem[];
  mediumViewOptions: MediumViewOptions;
}

export interface DispatchToProps {
  deleteItem: OnClickWithId;
  showHideAllByMedium: OnClickWith<Medium>;
  removeAllByMedium: OnClickWith<Medium>;
  showHideMediumRows: OnClickWith<Medium>;
  toggleLine: OnClickWithId;
}

export interface OwnProps extends Visible {
  showHideLegend: OnClick;
}

const LegendComponent = withContent<DispatchToProps & StateToProps>(Legend);

const mapStateToProps = ({report}: RootState): StateToProps => {
  const {resolution, savedReports} = report;
  const legendItems = getLegendItems(report);
  return ({
    legendItems,
    hasContent: legendItems.length > 0,
    mediumViewOptions: getMediumViewOptions(report),
    resolution,
    savedReports,
    timePeriod: {period: Period.latest}, // TODO timePeriod is unused but I could not exclude it from ReportState
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  deleteItem,
  showHideAllByMedium,
  removeAllByMedium,
  showHideMediumRows,
  toggleLine,
}, dispatch);

export const LegendContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(LegendComponent);
