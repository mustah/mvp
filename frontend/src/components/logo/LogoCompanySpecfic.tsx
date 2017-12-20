import * as React from 'react';
import {companySpecificLogin} from '../../app/routes';
import {ClassNamed} from '../../types/Types';
import './Logo.scss';
import classNames = require('classnames');

export const LogoCompanySpecific = ({className, company}: ClassNamed & {company: string}) => {
  return (
    <img src={companySpecificLogin[company]} className={classNames('Logo', className)}/>
  );
};
