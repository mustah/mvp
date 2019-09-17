import {connect} from 'react-redux';
import {RootState} from '../../../reducers/rootReducer';
import {CollectionContent, Props} from '../components/CollectionContent';

const mapStateToProps = ({
  ui: {
    pagination: {collectionStatFacilities: {totalElements}},
    toolbar: {collection: {view}},
  },
}: RootState): Props => ({hasContent: totalElements > 0, totalElements, view});

export const CollectionContentContainer = connect(mapStateToProps)(CollectionContent);
