import Divider from 'material-ui/Divider';
import ActionDelete from 'material-ui/svg-icons/action/delete';
import ImageEdit from 'material-ui/svg-icons/image/edit';
import * as React from 'react';
import {routes} from '../../../../app/routes';
import {actionMenuItemIconStyle, dividerStyle} from '../../../../app/themes';
import {ActionMenuItem} from '../../../../components/actions-dropdown/ActionMenuItem';
import {ActionsDropdown} from '../../../../components/actions-dropdown/ActionsDropdown';
import {Link} from '../../../../components/links/Link';
import {translate} from '../../../../services/translationService';
import {OnClick, OnClickWithId, RenderFunction, uuid} from '../../../../types/Types';

interface Props {
  id: uuid;
  confirmDelete: OnClickWithId;
}

export const MeterDefinitionActions = ({id, confirmDelete}: Props) => {

  const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => {
    const onClickDelete = () => {
      onClick();
      confirmDelete(id);
    };
    return [
      (
        <Link to={`${routes.adminMeterDefinitionsModify}/${id}`} key={`edit-${id}`}>
          <ActionMenuItem
            leftIcon={<ImageEdit style={actionMenuItemIconStyle}/>}
            name={translate('edit meter definition')}
            onClick={onClick}
          />
        </Link>
      ),
      (<Divider style={dividerStyle} key={`divider-meter-definition-admin-${id}`}/>),
      (
        <ActionMenuItem
          leftIcon={<ActionDelete style={actionMenuItemIconStyle}/>}
          name={translate('delete meter definition')}
          onClick={onClickDelete}
          key={`delete-${id}`}
        />
      ),
    ];
  };

  return (<ActionsDropdown renderPopoverContent={renderPopoverContent}/>);
};
