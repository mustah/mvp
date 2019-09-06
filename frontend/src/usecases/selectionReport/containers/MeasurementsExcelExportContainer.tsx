import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {ReportSector} from '../../../state/report/reportModels';
import {exportToExcelSuccess} from '../../../state/ui/graph/measurement/measurementActions';
import {hasMeasurementValues} from '../../../state/ui/graph/measurement/measurementSelectors';
import {DispatchToProps, MeasurementsExcelExport, StateToProps} from '../../report/components/MeasurementsExcelExport';

const mapStateToProps = ({selectionMeasurement: measurement}: RootState): StateToProps =>
  ({
    hasContent: hasMeasurementValues(measurement.measurementResponse) && !measurement.isFetching,
    measurement,
  });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  exportToExcelSuccess: exportToExcelSuccess(ReportSector.selectionReport),
}, dispatch);

export const MeasurementsExcelExportContainer = connect(mapStateToProps, mapDispatchToProps)(MeasurementsExcelExport);
