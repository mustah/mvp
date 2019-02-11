import {connect} from 'react-redux';
import {RootState} from '../../../reducers/rootReducer';
import {ToolbarView} from '../../../state/ui/toolbar/toolbarModels';
import {MeasurementContent} from '../components/MeasurementContent';

interface StateToProps {
  view: ToolbarView;
}

export type Props = StateToProps;

const mapStateToProps = ({ui: {toolbar: {measurement: {view}}}}: RootState): StateToProps => ({view});

export const MeasurementContentContainer = connect<StateToProps>(mapStateToProps)(MeasurementContent);
