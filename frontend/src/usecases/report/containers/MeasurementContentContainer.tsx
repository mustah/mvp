import {connect} from 'react-redux';
import {RootState} from '../../../reducers/rootReducer';
import {ToolbarViewSettings} from '../../../state/ui/toolbar/toolbarModels';
import {MeasurementContent} from '../components/MeasurementContent';

export type Props = ToolbarViewSettings;

const mapStateToProps = ({ui: {toolbar: {measurement: {view}}}}: RootState): ToolbarViewSettings => ({view});

export const MeasurementContentContainer = connect<ToolbarViewSettings>(mapStateToProps)(MeasurementContent);
