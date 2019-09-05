import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {getMeasurementParameters} from '../../../state/report/reportSelectors';
import {fetchMeasurementsForReport} from '../../../state/ui/graph/measurement/measurementActions';
import {
  DispatchToProps,
  FetchMeasurementProps,
  StateToProps,
  useFetchMeasurements
} from '../../../state/ui/graph/measurement/measurementHook';
import {fetchUserSelections} from '../../../state/user-selection/userSelectionActions';
import {getMeterParameters} from '../../../state/user-selection/userSelectionSelectors';
import {ReportMeasurementsExportExcelContainer} from './ReportMeasurementsExportExcelContainer';

const ReportMeasurementsExcelExport = (props: FetchMeasurementProps) => {
  useFetchMeasurements(props);

  return <ReportMeasurementsExportExcelContainer/>;
};

const mapStateToProps = (rootState: RootState): StateToProps => {
  const {measurement, userSelection: {userSelection}} = rootState;
  return ({
    parameters: getMeterParameters({userSelection}),
    measurementParameters: getMeasurementParameters(rootState),
    shouldFetchMeasurements: measurement.isExportingToExcel,
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchMeasurements: fetchMeasurementsForReport,
  fetchUserSelections,
}, dispatch);

export const ReportMeasurementsExcelExportContainer =
  connect(mapStateToProps, mapDispatchToProps)(ReportMeasurementsExcelExport);
