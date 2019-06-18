import {connect} from 'react-redux';
import {RootState} from '../../../reducers/rootReducer';
import {ToolbarViewSettingsProps} from '../../../state/ui/toolbar/toolbarModels';
import {CollectionContent} from '../components/CollectionContent';

const mapStateToProps = ({ui: {toolbar: {collection: {view}}}}: RootState): ToolbarViewSettingsProps => ({view});

export const CollectionContentContainer = connect(mapStateToProps)(CollectionContent);
