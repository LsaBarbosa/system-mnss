package br.com.novaalianca.mnss.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.testcontainers.utility.DockerImageName;

class QualityToolingTest {
    @Test
    void junitMockitoTestcontainersAndMapstructAreAvailable() {
        Runnable runnable = mock(Runnable.class);

        runnable.run();

        verify(runnable).run();
        assertThat(DockerImageName.parse("postgres:17").getRepository()).isEqualTo("postgres");
        assertThat(SampleMapper.INSTANCE.map(new SourceDto("Nova Alianca")).name()).isEqualTo("Nova Alianca");
    }

    @Mapper
    interface SampleMapper {
        SampleMapper INSTANCE = Mappers.getMapper(SampleMapper.class);

        TargetDto map(SourceDto source);
    }

    record SourceDto(String name) {
    }

    record TargetDto(String name) {
    }
}
