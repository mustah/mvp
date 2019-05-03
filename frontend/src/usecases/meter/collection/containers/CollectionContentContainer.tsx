import {connect} from 'react-redux';
import {RootState} from '../../../../reducers/rootReducer';
import {ToolbarViewSettings} from '../../../../state/ui/toolbar/toolbarModels';
import {OwnProps} from '../../measurements/meterDetailModels';
import {CollectionContent} from '../components/CollectionContent';

const mapStateToProps = ({ui: {toolbar: {meterCollection: {view}}}}: RootState): ToolbarViewSettings => ({view});

export const CollectionContentContainer =
  connect<ToolbarViewSettings, null, OwnProps>(mapStateToProps)(CollectionContent);
