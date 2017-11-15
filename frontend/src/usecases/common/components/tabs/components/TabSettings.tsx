import * as React from 'react';
import {translate} from '../../../../../services/translationService';
import {ActionsDropdown} from '../../actions-dropdown/ActionsDropdown';
import {Column} from '../../layouts/column/Column';
import {TabUnderline} from './TabUnderliner';

export const TabSettings = () => {
  const actions = [translate('export to Excel (.csv)'), translate('export to JSON')];
  return (
    <Column className="TabSettings">
      <ActionsDropdown actions={actions} className="flex-1 Row-right"/>
      <TabUnderline/>
    </Column>
  );
};
