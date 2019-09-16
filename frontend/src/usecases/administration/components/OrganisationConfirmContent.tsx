import * as React from 'react';
import {ConfirmDialog} from '../../../components/dialog/DeleteConfirmDialog';
import {RequestParameter} from '../../../helpers/urlFactory';
import {EndPoints} from '../../../services/endPoints';
import {DispatchToProps, OwnProps, StateToProps} from '../containers/OrganisationConfirmContainer';

type Props = OwnProps & StateToProps & DispatchToProps;

export const OrganisationConfirmContent = ({
  isOpen,
  close,
  confirm,
  text,
  headline,
  id,
  fetchOrganisationSummary,
  fetchSubOrganisations,
  isSuccessfullyFetched,
  clearSubOrganisations,
  clearOrganisationSummary,

}: Props) => {
  React.useEffect(() => {
    if (id && isOpen) {
      fetchSubOrganisations(`${RequestParameter.organisation}=${id}`);
      fetchOrganisationSummary(EndPoints.summaryMeters, `${RequestParameter.organisation}=${id}`);
    }
  }, [id, isOpen]);

  const onClose = () => {
    clearSubOrganisations();
    clearOrganisationSummary();
    close();
  };

  const onConfirm = () => {
    clearSubOrganisations();
    clearOrganisationSummary();
    confirm();
  };

  return isOpen && isSuccessfullyFetched ? (
    <ConfirmDialog
      isOpen={isOpen}
      close={onClose}
      confirm={onConfirm}
      text={text}
      headline={headline}
    />
  ) : <></>;
};
