import * as React from 'react';
import {routes} from '../../app/routes';
import {history} from '../../index';
import {translate} from '../../services/translationService';
import {IdNamed, OnClick, OnClickWithId, RenderFunction} from '../../types/Types';
import {connectedSuperAdminOnly} from '../hoc/withRoles';
import {ActionMenuItem, ActionMenuItemProps} from './ActionMenuItem';
import {ActionsDropdown} from './ActionsDropdown';

interface Props {
  item: IdNamed;
  selectEntryAdd: OnClickWithId;
  syncWithMetering?: OnClickWithId;
}

const SyncWithMeteringMenuItem = connectedSuperAdminOnly<ActionMenuItemProps>(ActionMenuItem);

export const ListActionsDropdown = (props: Props) => {
  const {item: {id}, selectEntryAdd, syncWithMetering} = props;

  const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => {
    const onAddToReport = () => {
      onClick();
      history.push(`${routes.report}/${id}`);
      selectEntryAdd(id);
    };

    const menuItemProps: ActionMenuItemProps = {
      name: translate('sync'),
      onClick: () => {
        onClick();
        if (syncWithMetering) {
          syncWithMetering(id);
        }
      },
    };
    return ([
      <SyncWithMeteringMenuItem {...menuItemProps} key={`sync-${id}`}/>,
      <ActionMenuItem name={translate('add to report')} onClick={onAddToReport} key={`add-to-report-${id}`}/>,
    ]);
  };

  return (<ActionsDropdown renderPopoverContent={renderPopoverContent}/>);
};
