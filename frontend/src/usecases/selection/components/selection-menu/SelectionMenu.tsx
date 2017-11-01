import * as React from 'react';
import {translate} from '../../../../services/translationService';
import {Clickable} from '../../../../types/Types';
import {CloseIcon} from '../../../common/components/icons/IconClose';
import {RowCenter} from '../../../common/components/layouts/row/Row';
import {Normal} from '../../../common/components/texts/Texts';

export const SelectionMenu = (props: Clickable) => (
  <RowCenter>
    <CloseIcon onClick={props.onClick}/>
    <Normal className="Italic clickable">{translate('new search')}*</Normal>
  </RowCenter>
);
