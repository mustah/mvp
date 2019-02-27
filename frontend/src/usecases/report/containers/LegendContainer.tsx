import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Period} from '../../../components/dates/dateModels';
import {withContent} from '../../../components/hoc/withContent';
import {RootState} from '../../../reducers/rootReducer';
import {Medium} from '../../../state/ui/graph/measurement/measurementModels';
import {HasContent, OnClick, OnClickWith, OnClickWithId, Visible} from '../../../types/Types';
import {Legend} from '../components/Legend';
import {
  deleteItem,
  removeAllByMedium,
  showHideAllByMedium,
  showHideMediumRows,
  toggleLine,
  toggleQuantityByMedium
} from '../reportActions';
import {LegendItem, MediumViewOptions, QuantityMedium, ReportState} from '../reportModels';
import {getLegendItems, getMediumViewOptions, hasLegendItems} from '../reportSelectors';

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
  toggleQuantityByMedium: OnClickWith<QuantityMedium>;
}

export interface OwnProps extends Visible {
  showHideLegend: OnClick;
}

const LegendComponent = withContent<DispatchToProps & StateToProps>(Legend);

const mapStateToProps = ({report}: RootState): StateToProps => {
  const {resolution, savedReports} = report;
  return ({
    legendItems: getLegendItems(report),
    hasContent: hasLegendItems(report),
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
  toggleQuantityByMedium,
}, dispatch);

export const LegendContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(LegendComponent);
