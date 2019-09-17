import {first} from 'lodash';
import * as React from 'react';
import {ButtonSecondary} from '../../../../../components/buttons/ButtonSecondary';
import {RowBottom} from '../../../../../components/layouts/row/Row';
import {InfoText} from '../../../../../components/texts/Texts';
import {firstUpperTranslated} from '../../../../../services/translationService';
import {OnClickWith} from '../../../../../types/Types';

const fileTypes = '.txt, .csv';

interface Props {
  onLoadEnd: OnClickWith<string>;
}

export const DeviceIdsFileUploader = ({onLoadEnd}: Props) => {
  const inputRef = React.useRef<HTMLInputElement>(null);

  const readFileContent = (ev: React.ChangeEvent<HTMLInputElement>) => {
    const files = first(ev.target.files);
    if (files) {
      const fileReader: FileReader = new FileReader();
      fileReader.readAsText(files);
      fileReader.onloadend = _ => onLoadEnd(fileReader.result as string);
    }
  };

  const onSelectFile = _ => {
    const element = inputRef.current;
    if (element) {
      element.value = '';
      element.click();
    }
  };

  return (
    <RowBottom style={{marginBottom: 20}}>
      <input
        type="file"
        ref={inputRef}
        accept={fileTypes}
        onChange={readFileContent}
        style={{display: 'none'}}
      />
      <ButtonSecondary
        className="flex-align-self-start"
        label={firstUpperTranslated('choose file')}
        onClick={onSelectFile}
      />
      <InfoText style={{marginLeft: 16}}>
        {`${firstUpperTranslated('comma separated device euis from file')} (${fileTypes})`}
      </InfoText>
    </RowBottom>
  );
};
