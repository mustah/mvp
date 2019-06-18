import {connect} from 'react-redux';
import {RootState} from '../../../../reducers/rootReducer';
import {ToolbarViewSettingsProps} from '../../../../state/ui/toolbar/toolbarModels';
import {OwnProps} from '../../measurements/meterDetailModels';
import {CollectionContent} from '../components/CollectionContent';

const mapStateToProps = ({ui: {toolbar: {meterCollection: {view}}}}: RootState): ToolbarViewSettingsProps => ({view});

export const CollectionContentContainer =
  connect<ToolbarViewSettingsProps, null, OwnProps>(mapStateToProps)(CollectionContent);
