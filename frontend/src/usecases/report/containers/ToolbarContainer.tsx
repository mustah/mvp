import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {TemporalResolution} from '../../../components/dates/dateModels';
import {RootState} from '../../../reducers/rootReducer';
import {changeToolbarView} from '../../../state/ui/toolbar/toolbarActions';
import {OnChangeToolbarView, ToolbarView} from '../../../state/ui/toolbar/toolbarModels';
import {OnSelectResolution} from '../../../state/user-selection/userSelectionModels';
import {OnClick} from '../../../types/Types';
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

interface OwnProps {
  toggleLegend?: OnClick;
}

export type Props = StateToProps & DispatchToProps & OwnProps;

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
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(Toolbar);
