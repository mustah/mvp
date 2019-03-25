import {connect} from 'react-redux';
import {RouteComponentProps} from 'react-router';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {CallbackWith} from '../../../types/Types';
import {SearchResult} from '../components/SearchResult';
import {validationSearch} from '../../../state/search/searchActions';

interface StateToProps {
  queryInState?: string;
}

interface DispatchToProps {
  validationSearch: CallbackWith<string>;
}

type OwnProps = RouteComponentProps<{searchQuery: string}>;

export type SearchResultProps = OwnProps & StateToProps & DispatchToProps;

const mapStateToProps = ({
  search: {validation: {query}}
}: RootState): StateToProps => ({
  queryInState: query,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  validationSearch,
}, dispatch);

export const SearchResultContainer = connect<StateToProps, DispatchToProps, OwnProps>(
  mapStateToProps, mapDispatchToProps
)(SearchResult);
