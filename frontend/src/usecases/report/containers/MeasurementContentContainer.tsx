import {connect} from 'react-redux';
import {RootState} from '../../../reducers/rootReducer';
import {ToolbarViewSettingsProps} from '../../../state/ui/toolbar/toolbarModels';
import {MeasurementContent} from '../components/MeasurementContent';

const mapStateToProps = ({ui: {toolbar: {measurement: {view}}}}: RootState): ToolbarViewSettingsProps => ({view});

export const MeasurementContentContainer = connect(mapStateToProps)(MeasurementContent);
