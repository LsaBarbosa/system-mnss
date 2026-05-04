import { Pipe, type PipeTransform } from '@angular/core';

@Pipe({
  name: 'mnssBackupDate',
  standalone: true
})
export class BackupDatePipe implements PipeTransform {
  transform(value: Date | string | null | undefined): string {
    if (!value) {
      return 'Sem backup';
    }

    const date = value instanceof Date ? value : new Date(value);
    if (Number.isNaN(date.getTime())) {
      return 'Data invalida';
    }

    return new Intl.DateTimeFormat('pt-BR', {
      dateStyle: 'short',
      timeStyle: 'short',
      timeZone: 'America/Sao_Paulo'
    }).format(date);
  }
}
