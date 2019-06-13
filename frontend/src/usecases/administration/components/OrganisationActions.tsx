import Divider from 'material-ui/Divider';
import ActionDelete from 'material-ui/svg-icons/action/delete';
import ImageEdit from 'material-ui/svg-icons/image/edit';
import NotificationSync from 'material-ui/svg-icons/notification/sync';
import * as React from 'react';
import {routes} from '../../../app/routes';
import {actionMenuItemIconStyle, dividerStyle} from '../../../app/themes';
import {ActionMenuItem, ActionMenuItemProps} from '../../../components/actions-dropdown/ActionMenuItem';
import {ActionsDropdown} from '../../../components/actions-dropdown/ActionsDropdown';
import {withSuperAdminOnly} from '../../../components/hoc/withRoles';
import {Link} from '../../../components/links/Link';
import {StoreProvider} from '../../../components/popover/StoreProvider';
import {translate} from '../../../services/translationService';
import {OnClick, OnClickWithId, RenderFunction, uuid} from '../../../types/Types';

const SyncWithMeteringMenuItem = withSuperAdminOnly<ActionMenuItemProps>(ActionMenuItem);

interface Props {
  id: uuid;
  confirmDelete: OnClickWithId;
  syncMetersOrganisation: OnClickWithId;
}

export const OrganisationActions = ({id, confirmDelete, syncMetersOrganisation}: Props) => {

  const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => {
    const syncMetersOrganisationProps: ActionMenuItemProps = {
      name: translate('sync all meters for this organisation'),
      onClick: () => {
        onClick();
        syncMetersOrganisation(id);
      },
      leftIcon: <NotificationSync style={actionMenuItemIconStyle}/>,
    };

    const onClickDelete = () => {
      onClick();
      confirmDelete(id);
    };
    return [
      (
        <Link to={`${routes.adminOrganisationsModify}/${id}`} key={`edit-${id}`}>
          <ActionMenuItem
            leftIcon={<ImageEdit style={actionMenuItemIconStyle}/>}
            name={translate('edit organisation')}
            onClick={onClick}
          />
        </Link>
      ),
      (
        <StoreProvider key="sync-meters-organisation-menu-item">
          <SyncWithMeteringMenuItem {...syncMetersOrganisationProps}/>
        </StoreProvider>
      ),
      (<Divider style={dividerStyle} key={`divider-organisations-admin-${id}`}/>),
      (
        <ActionMenuItem
          leftIcon={<ActionDelete style={actionMenuItemIconStyle}/>}
          name={translate('delete organisation')}
          onClick={onClickDelete}
          key={`delete-${id}`}
        />
      ),
    ];
  };

  return (<ActionsDropdown renderPopoverContent={renderPopoverContent}/>);
};
