import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {TemporalResolution} from '../../../components/dates/dateModels';
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
  toggleQuantityById,
  toggleQuantityByMedium
} from '../reportActions';
import {
  LegendItem,
  MediumViewOptions,
  QuantityId,
  QuantityMedium,
  SavedReportsState,
  SelectedQuantityColumns
} from '../reportModels';
import {getLegendItems, getMediumViewOptions, getSelectedQuantityColumns, hasLegendItems} from '../reportSelectors';

export interface StateToProps extends HasContent {
  legendItems: LegendItem[];
  mediumViewOptions: MediumViewOptions;
  resolution: TemporalResolution;
  savedReports: SavedReportsState;
  selectedQuantityColumns: SelectedQuantityColumns;
}

export interface DispatchToProps {
  deleteItem: OnClickWithId;
  removeAllByMedium: OnClickWith<Medium>;
  showHideAllByMedium: OnClickWith<Medium>;
  showHideMediumRows: OnClickWith<Medium>;
  toggleLine: OnClickWithId;
  toggleQuantityByMedium: OnClickWith<QuantityMedium>;
  toggleQuantityById: OnClickWith<QuantityId>;
}

export interface OwnProps extends Visible {
  showHideLegend: OnClick;
}

const LegendComponent = withContent<DispatchToProps & StateToProps>(Legend);

const mapStateToProps = ({report}: RootState): StateToProps => {
  const {temporal: {resolution}, savedReports} = report;
  return ({
    hasContent: hasLegendItems(savedReports),
    legendItems: getLegendItems(savedReports),
    mediumViewOptions: getMediumViewOptions(savedReports),
    resolution,
    savedReports,
    selectedQuantityColumns: getSelectedQuantityColumns(savedReports),
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  deleteItem,
  showHideAllByMedium,
  removeAllByMedium,
  showHideMediumRows,
  toggleLine,
  toggleQuantityByMedium,
  toggleQuantityById,
}, dispatch);

export const LegendContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(LegendComponent);
