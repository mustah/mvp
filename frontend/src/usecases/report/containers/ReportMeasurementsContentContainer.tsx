import {connect} from 'react-redux';
import {RootState} from '../../../reducers/rootReducer';
import {ToolbarViewSettingsProps} from '../../../state/ui/toolbar/toolbarModels';
import {MeasurementsContent} from '../components/MeasurementsContent';

const mapStateToProps = ({ui: {toolbar: {measurement: {view}}}}: RootState): ToolbarViewSettingsProps => ({view});

export const ReportMeasurementsContentContainer = connect(mapStateToProps)(MeasurementsContent);
