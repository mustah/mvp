import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {isMetersPageFetching} from '../../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {addAllToReport} from '../../../state/report/reportActions';
import {ReportSector} from '../../../state/report/reportModels';
import {getHiddenLines, getSelectionMeasurementParameters, hasLegendItems} from '../../../state/report/reportSelectors';
import {
  exportSelectionReportToExcel,
  fetchMeasurementsForSelectionReport,
  measurementClearError
} from '../../../state/ui/graph/measurement/measurementActions';
import {hasMeasurementValues} from '../../../state/ui/graph/measurement/measurementSelectors';
import {isSideMenuOpen} from '../../../state/ui/uiSelectors';
import {fetchUserSelections} from '../../../state/user-selection/userSelectionActions';
import {getMeterParameters, getUserSelectionId} from '../../../state/user-selection/userSelectionSelectors';
import {MeasurementLineChart} from '../../report/components/MeasurementLineChart';
import {Measurements} from '../../report/components/Measurements';
import {DispatchToProps, StateToProps} from '../../report/containers/MeasurementsContainer';

const mapStateToProps = (rootState: RootState): StateToProps => {
  const {
    selectionReport: {savedReports},
    domainModels: {userSelections},
    paginatedDomainModels: {meters},
    selectionMeasurement,
    userSelection: {userSelection},
    ui
  } = rootState;
  return ({
    hasLegendItems: hasLegendItems(savedReports),
    hasContent: hasMeasurementValues(selectionMeasurement.measurementResponse),
    hiddenLines: getHiddenLines(savedReports),
    isFetching: selectionMeasurement.isFetching || isMetersPageFetching(meters, ui.pagination),
    isSideMenuOpen: isSideMenuOpen(ui),
    measurement: selectionMeasurement,
    parameters: getMeterParameters({userSelection}),
    requestParameters: getSelectionMeasurementParameters(rootState),
    userSelectionId: getUserSelectionId(rootState.userSelection),
    userSelections,
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  addAllToReport,
  clearError: measurementClearError(ReportSector.selectionReport),
  exportToExcelSuccess: exportSelectionReportToExcel,
  fetchMeasurements: fetchMeasurementsForSelectionReport,
  fetchUserSelections,
}, dispatch);

export const GraphContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(MeasurementLineChart);

export const MeasurementsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(Measurements);
