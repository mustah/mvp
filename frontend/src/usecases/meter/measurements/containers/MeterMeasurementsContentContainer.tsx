import {connect} from 'react-redux';
import {RootState} from '../../../../reducers/rootReducer';
import {ToolbarViewSettings} from '../../../../state/ui/toolbar/toolbarModels';
import {MeterMeasurementsContent} from '../components/MeterMeasurementsContent';
import {OwnProps} from '../meterDetailModels';

const mapStateToProps = ({ui: {toolbar: {meterMeasurement: {view}}}}: RootState): ToolbarViewSettings => ({view});

export const MeterMeasurementsContentContainer =
  connect<ToolbarViewSettings, null, OwnProps>(mapStateToProps)(MeterMeasurementsContent);
