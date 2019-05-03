import {connect} from 'react-redux';
import {RootState} from '../../../reducers/rootReducer';
import {ToolbarViewSettings} from '../../../state/ui/toolbar/toolbarModels';
import {CollectionContent} from '../components/CollectionContent';

const mapStateToProps = ({ui: {toolbar: {collection: {view}}}}: RootState): ToolbarViewSettings => ({view});

export const CollectionStatContentContainer = connect<ToolbarViewSettings>(mapStateToProps)(CollectionContent);
