import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/index';
import {Bold} from '../../common/components/texts/Texts';
import {fetchCollections} from '../collectionActions';
import {CollectionState} from '../models/Collections';

export interface CollectionContainerProps {
  fetchCollections: () => any;
  collection: CollectionState;
}

const CollectionContainer = (props: CollectionContainerProps) => {
  const {title} = props.collection;
  return (
    <div>
      <Bold>{title}</Bold>
    </div>
  );
};

const mapStateToProps = (state: RootState) => {
  const {collection} = state;
  return {
    collection,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  fetchCollections,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(CollectionContainer);
