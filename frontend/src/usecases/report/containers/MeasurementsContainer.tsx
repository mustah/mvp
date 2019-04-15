import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {isMetersPageFetching} from '../../../state/domain-models-paginated/paginatedDomainModelsSelectors';
import {NormalizedState} from '../../../state/domain-models/domainModels';
import {addAllToReport} from '../../../state/report/reportActions';
import {LegendItem, ReportSector} from '../../../state/report/reportModels';
import {getHiddenLines, getMeasurementParameters, hasLegendItems} from '../../../state/report/reportSelectors';
import {
  exportToExcelSuccess,
  fetchMeasurementsForReport,
  measurementClearError
} from '../../../state/ui/graph/measurement/measurementActions';
import {
  FetchMeasurements,
  MeasurementParameters,
  MeasurementState
} from '../../../state/ui/graph/measurement/measurementModels';
import {hasMeasurementValues} from '../../../state/ui/graph/measurement/measurementSelectors';
import {isSideMenuOpen} from '../../../state/ui/uiSelectors';
import {fetchUserSelections} from '../../../state/user-selection/userSelectionActions';
import {UserSelection} from '../../../state/user-selection/userSelectionModels';
import {getMeterParameters, getUserSelectionId} from '../../../state/user-selection/userSelectionSelectors';
import {Callback, CallbackWith, EncodedUriParameters, Fetch, OnClick, uuid} from '../../../types/Types';
import {MeasurementLineChart} from '../components/MeasurementLineChart';
import {Measurements} from '../components/Measurements';

export interface StateToProps {
  hasLegendItems: boolean;
  hasContent: boolean;
  hiddenLines: uuid[];
  isFetching: boolean;
  isSideMenuOpen: boolean;
  measurement: MeasurementState;
  parameters: EncodedUriParameters;
  requestParameters: MeasurementParameters;
  userSelections: NormalizedState<UserSelection>;
  userSelectionId: uuid;
}

export interface DispatchToProps {
  addAllToReport: CallbackWith<LegendItem[]>;
  clearError: OnClick;
  exportToExcelSuccess: Callback;
  fetchMeasurements: FetchMeasurements;
  fetchUserSelections: Fetch;
}

const mapStateToProps = (rootState: RootState): StateToProps => {
  const {
    domainModels: {userSelections},
    paginatedDomainModels: {meters},
    report: {savedReports},
    measurement,
    userSelection: {userSelection},
    ui,
  } = rootState;
  return ({
    hasLegendItems: hasLegendItems(savedReports),
    hasContent: hasMeasurementValues(measurement.measurementResponse),
    hiddenLines: getHiddenLines(savedReports),
    isFetching: measurement.isFetching || isMetersPageFetching(meters, ui.pagination),
    isSideMenuOpen: isSideMenuOpen(ui),
    measurement,
    parameters: getMeterParameters({userSelection}),
    requestParameters: getMeasurementParameters(rootState),
    userSelectionId: getUserSelectionId(rootState.userSelection),
    userSelections,
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  addAllToReport,
  clearError: measurementClearError(ReportSector.report),
  exportToExcelSuccess: exportToExcelSuccess(ReportSector.report),
  fetchMeasurements: fetchMeasurementsForReport,
  fetchUserSelections,
}, dispatch);

export const GraphContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(MeasurementLineChart);

export const MeasurementsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(Measurements);
