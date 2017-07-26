import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/index';
import {SelectionsOverview} from '../../dashboard/components/SelectionsOverview';
import {fetchCollections} from '../collectionActions';
import {CollectionState} from '../models/Collections';

export interface CollectionContainerProps {
  fetchCollections: () => any;
  collection: CollectionState;
}

const CollectionContainer = (props: CollectionContainerProps) => {
  const {fetchCollections} = props;
  return (
    <div>
      <SelectionsOverview title={'Allt'}/>

      <div className="button" onClick={fetchCollections}>COLLECTIONS</div>
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
