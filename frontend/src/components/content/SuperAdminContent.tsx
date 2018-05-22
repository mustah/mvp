import * as React from 'react';
import {superAdminComponent} from '../../helpers/hoc';
import {Children} from '../../types/Types';

interface Props {
  children?: Children;
}

const SuperAdminContent = ({children}: Props) => <div>{children}</div>;

export const SuperAdminComponent = superAdminComponent<Props>(SuperAdminContent);
