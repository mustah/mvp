import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../../reducers/rootReducer';
import {
  DispatchToProps,
  MeasurementsExcelExport,
  StateToProps
} from '../../../report/components/MeasurementsExcelExport';
import {meterDetailExportToExcelSuccess} from '../meterDetailMeasurementActions';
import {hasMeasurementValues} from '../meterDetailMeasurementsSelectors';

const mapStateToProps = ({domainModels: {meterDetailMeasurement: measurement}}: RootState): StateToProps =>
  ({
    hasContent: hasMeasurementValues(measurement.measurementResponse),
    measurement,
  });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  exportToExcelSuccess: meterDetailExportToExcelSuccess,
}, dispatch);

export const MeterMeasurementsExcelExportContainer =
  connect(mapStateToProps, mapDispatchToProps)(MeasurementsExcelExport);
