import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {ReportSector} from '../../../state/report/reportModels';
import {exportToExcelSuccess} from '../../../state/ui/graph/measurement/measurementActions';
import {hasMeasurementValues} from '../../../state/ui/graph/measurement/measurementSelectors';
import {getMeterParameters} from '../../../state/user-selection/userSelectionSelectors';
import {DispatchToProps, MeasurementsExcelExport, StateToProps} from '../components/MeasurementsExcelExport';

const mapStateToProps = ({measurement, userSelection: {userSelection}}: RootState): StateToProps =>
  ({
    hasContent: hasMeasurementValues(measurement.measurementResponse),
    measurement,
    parameters: getMeterParameters({userSelection}),
  });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  exportToExcelSuccess: exportToExcelSuccess(ReportSector.report),
}, dispatch);

export const ReportMeasurementsExportExcelContainer =
  connect(mapStateToProps, mapDispatchToProps)(MeasurementsExcelExport);
