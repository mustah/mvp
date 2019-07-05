import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {isMetersPageFetching} from '../../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {ReportSector} from '../../../state/report/reportModels';
import {
  getHiddenLines,
  getMeasurementParameters,
  getSelectedQuantities,
  getVisibilitySummary,
  hasLegendItems
} from '../../../state/report/reportSelectors';
import {
  fetchMeasurementsForReport,
  measurementClearError
} from '../../../state/ui/graph/measurement/measurementActions';
import {
  FetchMeasurements,
  MeasurementParameters,
  MeasurementState,
  Quantity
} from '../../../state/ui/graph/measurement/measurementModels';
import {hasMeasurementValues} from '../../../state/ui/graph/measurement/measurementSelectors';
import {isSideMenuOpen} from '../../../state/ui/uiSelectors';
import {fetchUserSelections} from '../../../state/user-selection/userSelectionActions';
import {ThresholdQuery} from '../../../state/user-selection/userSelectionModels';
import {
  getMeterParameters,
  getThreshold,
  getUserSelectionId
} from '../../../state/user-selection/userSelectionSelectors';
import {EncodedUriParameters, Fetch, OnClick, uuid} from '../../../types/Types';
import {MeasurementLineChart} from '../components/MeasurementLineChart';
import {Measurements} from '../components/Measurements';
import {VisibilitySummaryProps} from '../components/VisibilitySummary';

export interface StateToProps {
  hasLegendItems: boolean;
  hasContent: boolean;
  hiddenLines: uuid[];
  isFetching: boolean;
  isSideMenuOpen: boolean;
  measurement: MeasurementState;
  parameters: EncodedUriParameters;
  measurementParameters: MeasurementParameters;
  visibilitySummary?: VisibilitySummaryProps;
  threshold?: ThresholdQuery;
  userSelectionId: uuid;
  selectedQuantities: Quantity[];
  shouldFetchMeasurements: boolean;
}

export interface DispatchToProps {
  clearError: OnClick;
  fetchMeasurements: FetchMeasurements;
  fetchUserSelections: Fetch;
}

const mapStateToProps = (rootState: RootState): StateToProps => {
  const {
    domainModels: {userSelections},
    measurement,
    paginatedDomainModels: {meters},
    report,
    userSelection,
    ui,
  } = rootState;
  return ({
    hasLegendItems: hasLegendItems(report.savedReports),
    hasContent: hasMeasurementValues(measurement.measurementResponse),
    hiddenLines: getHiddenLines(report.savedReports),
    isFetching: measurement.isFetching || isMetersPageFetching(meters, ui.pagination),
    isSideMenuOpen: isSideMenuOpen(ui),
    measurement,
    parameters: getMeterParameters({userSelection: userSelection.userSelection}),
    measurementParameters: getMeasurementParameters(rootState),
    selectedQuantities: getSelectedQuantities(report.savedReports),
    shouldFetchMeasurements: userSelections.isSuccessfullyFetched,
    threshold: getThreshold(userSelection),
    userSelectionId: getUserSelectionId(userSelection),
    visibilitySummary: getVisibilitySummary(report.savedReports),
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  clearError: measurementClearError(ReportSector.report),
  fetchMeasurements: fetchMeasurementsForReport,
  fetchUserSelections,
}, dispatch);

export const GraphContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(MeasurementLineChart);

export const MeasurementsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(Measurements);
