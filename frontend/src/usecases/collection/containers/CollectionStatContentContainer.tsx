import {connect} from 'react-redux';
import {RootState} from '../../../reducers/rootReducer';
import {ToolbarView} from '../../../state/ui/toolbar/toolbarModels';
import {CollectionContent} from '../components/CollectionContent';

interface StateToProps {
  view: ToolbarView;
}

export type Props = StateToProps;

const mapStateToProps = ({ui: {toolbar: {collection: {view}}}}: RootState): StateToProps => ({view});

export const CollectionStatContentContainer = connect<StateToProps>(mapStateToProps)(CollectionContent);
