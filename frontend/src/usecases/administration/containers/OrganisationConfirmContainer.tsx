import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';

import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated} from '../../../services/translationService';
import {
  clearSubOrganisations,
  fetchSubOrganisations
} from '../../../state/domain-models/organisation/organisationsApiActions';
import {
  clearOrganisationSummary,
  fetchOrganisationSummary
} from '../../../state/organisation-summary/organisationSummaryApiActions';
import {Callback, Fetch, Identifiable, IdNamed, OnClick, OnFetch} from '../../../types/Types';
import {OrganisationConfirmContent} from '../components/OrganisationConfirmContent';

export interface OwnProps {
  isOpen: boolean;
  confirm: OnClick;
  close: OnClick;
  idName: IdNamed;
}

export interface StateToProps extends Identifiable {
  isOpen: boolean;
  confirm: OnClick;
  close: OnClick;
  text: string;
  headline: string;
  isSuccessfullyFetched: boolean;
}

export interface DispatchToProps {
  fetchOrganisationSummary: OnFetch;
  fetchSubOrganisations: Fetch;
  clearSubOrganisations: Callback;
  clearOrganisationSummary: Callback;
}

const mapStateToProps = (
  {
    organisationSummary: {numMeters, isSuccessfullyFetched: isSummarySuccessfullyFetched},
    domainModels: {subOrganisations: {total, isSuccessfullyFetched}}
  }: RootState,
  {isOpen, confirm, close, idName: {id, name}}: OwnProps
): StateToProps => {

  const headline: string = firstUpperTranslated(
    'are you sure you want to delete the organisation {{organisation}}',
    {organisation: name},
  );

  const text: string = firstUpperTranslated(
    'delete organisation {{meters}} {{subOrganisations}}',
    {meters: numMeters, subOrganisations: total},
  );

  return ({
    isOpen,
    confirm,
    close,
    headline,
    text,
    id,
    isSuccessfullyFetched: isSuccessfullyFetched && isSummarySuccessfullyFetched,
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchOrganisationSummary,
  fetchSubOrganisations,
  clearSubOrganisations,
  clearOrganisationSummary,
}, dispatch);

export const OrganisationConfirmContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(OrganisationConfirmContent);
