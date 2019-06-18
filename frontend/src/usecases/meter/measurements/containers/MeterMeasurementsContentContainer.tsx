import {connect} from 'react-redux';
import {RootState} from '../../../../reducers/rootReducer';
import {ToolbarViewSettingsProps} from '../../../../state/ui/toolbar/toolbarModels';
import {MeterMeasurementsContent} from '../components/MeterMeasurementsContent';
import {OwnProps} from '../meterDetailModels';

const mapStateToProps = ({ui: {toolbar: {meterMeasurement: {view}}}}: RootState): ToolbarViewSettingsProps => ({view});

export const MeterMeasurementsContentContainer =
  connect<ToolbarViewSettingsProps, null, OwnProps>(mapStateToProps)(MeterMeasurementsContent);
