import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {isMetersPageFetching} from '../../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {ReportSector} from '../../../state/report/reportModels';
import {
  getHiddenLines,
  getSelectedQuantities,
  getSelectionMeasurementParameters,
  getVisibilitySummary,
  hasLegendItems
} from '../../../state/report/reportSelectors';
import {
  fetchMeasurementsForSelectionReport,
  measurementClearError
} from '../../../state/ui/graph/measurement/measurementActions';
import {hasMeasurementValues} from '../../../state/ui/graph/measurement/measurementSelectors';
import {isSideMenuOpen} from '../../../state/ui/uiSelectors';
import {fetchUserSelections} from '../../../state/user-selection/userSelectionActions';
import {
  getMeterParameters,
  getThreshold,
  getUserSelectionId
} from '../../../state/user-selection/userSelectionSelectors';
import {MeasurementLineChart} from '../../report/components/MeasurementLineChart';
import {Measurements} from '../../report/components/Measurements';
import {DispatchToProps, StateToProps} from '../../report/containers/MeasurementsContainer';

const mapStateToProps = (rootState: RootState): StateToProps => {
  const {
    domainModels: {userSelections},
    paginatedDomainModels: {meters},
    selectionMeasurement,
    selectionReport,
    userSelection,
    ui
  } = rootState;
  return ({
    hasLegendItems: hasLegendItems(selectionReport.savedReports),
    hasContent: hasMeasurementValues(selectionMeasurement.measurementResponse),
    hiddenLines: getHiddenLines(selectionReport.savedReports),
    isFetching: selectionMeasurement.isFetching || isMetersPageFetching(meters, ui.pagination),
    isSideMenuOpen: isSideMenuOpen(ui),
    measurement: selectionMeasurement,
    parameters: getMeterParameters({userSelection: userSelection.userSelection}),
    measurementParameters: getSelectionMeasurementParameters(rootState),
    selectedQuantities: getSelectedQuantities(selectionReport.savedReports),
    shouldFetchMeasurements: userSelections.isSuccessfullyFetched,
    threshold: getThreshold(userSelection),
    userSelectionId: getUserSelectionId(userSelection),
    visibilitySummary: getVisibilitySummary(selectionReport.savedReports),
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  clearError: measurementClearError(ReportSector.selectionReport),
  fetchMeasurements: fetchMeasurementsForSelectionReport,
  fetchUserSelections,
}, dispatch);

export const MeasurementLineContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(MeasurementLineChart);

export const MeasurementsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(Measurements);
