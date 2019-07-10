import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {getAllSelectionMeasurementParameters} from '../../../state/report/reportSelectors';
import {fetchMeasurementsForSelectionReport as fetchMeasurements} from '../../../state/ui/graph/measurement/measurementActions';
import {
  DispatchToProps,
  FetchMeasurementProps,
  StateToProps,
  useFetchMeasurements
} from '../../../state/ui/graph/measurement/measurementHook';
import {fetchUserSelections} from '../../../state/user-selection/userSelectionActions';
import {getMeterParameters} from '../../../state/user-selection/userSelectionSelectors';
import {MeasurementsExcelExportContainer} from './MeasurementsExcelExportContainer';

const FetchMeasurementsExcelExport = (props: FetchMeasurementProps) => {
  useFetchMeasurements(props);
  return <MeasurementsExcelExportContainer/>;
};

const mapStateToProps = (rootState: RootState): StateToProps => {
  const {selectionMeasurement, userSelection: {userSelection}} = rootState;
  return ({
    parameters: getMeterParameters({userSelection}),
    measurementParameters: getAllSelectionMeasurementParameters(rootState),
    shouldFetchMeasurements: selectionMeasurement.isExportingToExcel,
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchMeasurements,
  fetchUserSelections,
}, dispatch);

export const SelectionMeasurementsExcelExportContainer =
  connect(mapStateToProps, mapDispatchToProps)(FetchMeasurementsExcelExport);
