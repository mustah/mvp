import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {TemporalResolution} from '../../../components/dates/dateModels';
import {withContent} from '../../../components/hoc/withContent';
import {RootState} from '../../../reducers/rootReducer';
import {HasContent, OnClick, OnClickWith, OnClickWithId, Visible} from '../../../types/Types';
import {Legend} from '../components/Legend';
import {makeColumnQuantities} from '../helpers/legendHelper';
import {
  deleteItem,
  removeAllByType,
  showHideAllByType,
  showHideLegendRows,
  toggleLine,
  toggleQuantityById,
  toggleQuantityByType
} from '../reportActions';
import {
  ColumnQuantities,
  LegendItem,
  LegendType,
  LegendViewOptions,
  QuantityId,
  QuantityLegendType,
  SavedReportsState,
  SelectedQuantityColumns
} from '../reportModels';
import {
  getLegendItems,
  getLegendViewOptions,
  getSelectedQuantityColumns,
  hasLegendItems
} from '../reportSelectors';

export interface StateToProps extends HasContent, ColumnQuantities {
  legendItems: LegendItem[];
  mediumViewOptions: LegendViewOptions;
  resolution: TemporalResolution;
  savedReports: SavedReportsState;
  selectedQuantityColumns: SelectedQuantityColumns;
}

export interface RowDispatch {
  removeAllByType: OnClickWith<LegendType>;
  showHideAllByType: OnClickWith<LegendType>;
  toggleQuantityByType: OnClickWith<QuantityLegendType>;
}

export interface DispatchToProps extends RowDispatch {
  deleteItem: OnClickWithId;
  showHideLegendRows: OnClickWith<LegendType>;
  toggleLine: OnClickWithId;
  toggleQuantityById: OnClickWith<QuantityId>;
}

export interface OwnProps extends Visible {
  showHideLegend: OnClick;
}

const LegendComponent = withContent<DispatchToProps & StateToProps>(Legend);

const mapStateToProps = ({report}: RootState): StateToProps => {
  const {temporal: {resolution}, savedReports} = report;
  return ({
    columnQuantities: makeColumnQuantities(savedReports),
    hasContent: hasLegendItems(savedReports),
    legendItems: getLegendItems(savedReports),
    mediumViewOptions: getLegendViewOptions(savedReports),
    resolution,
    savedReports,
    selectedQuantityColumns: getSelectedQuantityColumns(savedReports),
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  deleteItem,
  showHideAllByType,
  removeAllByType,
  showHideLegendRows,
  toggleLine,
  toggleQuantityByType,
  toggleQuantityById,
}, dispatch);

export const LegendContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(LegendComponent);
