import {connect} from 'react-redux';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {SearchResult} from '../components/SearchResult';

interface StateToProps {
  title: string;
}

const mapStateToProps = ({userSelection: {userSelection}}: RootState): StateToProps => ({
  title: userSelection.id === -1 ? translate('selection') : userSelection.name,
});

export const SearchResultContainer = connect<StateToProps>(mapStateToProps)(SearchResult);
