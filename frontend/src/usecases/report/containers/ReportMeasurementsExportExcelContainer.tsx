import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {ReportSector} from '../../../state/report/reportModels';
import {exportToExcelSuccess} from '../../../state/ui/graph/measurement/measurementActions';
import {hasMeasurementValues} from '../../../state/ui/graph/measurement/measurementSelectors';
import {DispatchToProps, MeasurementsExcelExport, StateToProps} from '../components/MeasurementsExcelExport';

const mapStateToProps = ({measurement}: RootState): StateToProps =>
  ({
    hasContent: hasMeasurementValues(measurement.measurementResponse),
    measurement,
  });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  exportToExcelSuccess: exportToExcelSuccess(ReportSector.report),
}, dispatch);

export const ReportMeasurementsExportExcelContainer =
  connect(mapStateToProps, mapDispatchToProps)(MeasurementsExcelExport);
