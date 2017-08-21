import * as React from 'react';
import {Icon} from '../../common/components/icons/Icons';
import {Bold, Small} from '../../common/components/texts/Texts';
import {Column} from '../../layouts/components/column/Column';
import {Row} from '../../layouts/components/row/Row';
import {MenuSeparator} from '../../topmenu/components/separators/MenuSeparator';
import './ProfileContainer.scss';

export const ProfileContainer = (props) => {
  return (
    <Column className="flex-1">
      <Row className="ProfileContainer">
        <Column>
          <Bold>Anna Johansson</Bold>
          <Small className="text-align-right">Bostäder AB</Small>
        </Column>
        <Icon name="account-circle"/>
      </Row>
      <MenuSeparator/>
    </Column>
  );
};
