import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {TemporalResolution} from '../../../components/dates/dateModels';
import {RootState} from '../../../reducers/rootReducer';
import {changeToolbarView} from '../../../state/ui/toolbar/toolbarActions';
import {OnChangeToolbarView, ToolbarView} from '../../../state/ui/toolbar/toolbarModels';
import {OnSelectResolution} from '../../../state/user-selection/userSelectionModels';
import {Toolbar} from '../components/Toolbar';
import {selectResolution} from '../reportActions';

interface StateToProps {
  resolution: TemporalResolution;
  view: ToolbarView;
}

interface DispatchToProps {
  changeToolbarView: OnChangeToolbarView;
  selectResolution: OnSelectResolution;
}

export type Props = StateToProps & DispatchToProps;

const mapStateToProps = ({
  report: {resolution},
  ui: {toolbar: {measurement: {view}}}
}: RootState): StateToProps =>
  ({resolution, view});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeToolbarView,
  selectResolution,
}, dispatch);

export const ToolbarContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(Toolbar);
