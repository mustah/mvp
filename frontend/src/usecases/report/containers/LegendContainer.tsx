import {connect} from 'react-redux';
import {compose} from 'recompose';
import {bindActionCreators} from 'redux';
import {withContent} from '../../../components/hoc/withContent';
import {ThemeContext, withCssStyles} from '../../../components/hoc/withThemeProvider';
import {RootState} from '../../../reducers/rootReducer';
import {
  deleteItem,
  removeAllByType,
  showHideAllByType,
  showHideLegendRows,
  toggleLine,
  toggleQuantityById,
  toggleQuantityByType
} from '../../../state/report/reportActions';
import {
  ColumnQuantities,
  LegendItem,
  LegendType,
  LegendViewOptions,
  QuantityId,
  QuantityLegendType,
  ReportSector,
  ResolutionAware,
  SavedReportsState,
  SelectedQuantities
} from '../../../state/report/reportModels';
import {
  getLegendItems,
  getLegendViewOptions,
  getSelectedQuantitiesMap,
  hasLegendItems
} from '../../../state/report/reportSelectors';
import {HasContent, OnClick, OnClickWith, OnClickWithId, Visible} from '../../../types/Types';
import {Legend} from '../components/Legend';
import {makeSelectableQuantities} from '../helpers/legendHelper';

export interface StateToProps extends HasContent, ColumnQuantities, ResolutionAware {
  legendItems: LegendItem[];
  mediumViewOptions: LegendViewOptions;
  savedReports: SavedReportsState;
  selectedQuantitiesMap: SelectedQuantities;
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

type WrapperProps = DispatchToProps & StateToProps & OwnProps;

const LegendComponent = compose<WrapperProps & ThemeContext, WrapperProps>(
  withCssStyles,
  withContent
)(Legend);

const mapStateToProps = ({report: {temporal: {resolution}, savedReports}}: RootState): StateToProps => ({
  columnQuantities: makeSelectableQuantities(savedReports),
  hasContent: hasLegendItems(savedReports),
  legendItems: getLegendItems(savedReports),
  mediumViewOptions: getLegendViewOptions(savedReports),
  resolution,
  savedReports,
  selectedQuantitiesMap: getSelectedQuantitiesMap(savedReports),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  deleteItem,
  removeAllByType: removeAllByType(ReportSector.report),
  showHideAllByType: showHideAllByType(ReportSector.report),
  showHideLegendRows: showHideLegendRows(ReportSector.report),
  toggleLine: toggleLine(ReportSector.report),
  toggleQuantityByType: toggleQuantityByType(ReportSector.report),
  toggleQuantityById: toggleQuantityById(ReportSector.report),
}, dispatch);

export const LegendContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(LegendComponent);
